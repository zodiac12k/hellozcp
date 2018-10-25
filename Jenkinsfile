@Library('retort-lib') _
def label = "jenkins-${UUID.randomUUID().toString()}"
 
def ZCP_USERID = 'earth1223'
def DOCKER_IMAGE = 'earth1223/hellozcp'
def K8S_NAMESPACE = 'earth1223'
def VERSION = 'develop'
 
podTemplate(label:label,
    serviceAccount: "zcp-system-sa-${ZCP_USERID}",
    containers: [
        containerTemplate(name: 'maven', image: 'maven:3.5.2-jdk-8-alpine', ttyEnabled: true, command: 'cat'),
        containerTemplate(name: 'docker', image: 'docker', ttyEnabled: true, command: 'cat'),
        containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl', ttyEnabled: true, command: 'cat')
    ],
    volumes: [
        hostPathVolume(hostPath: '/var/run/docker.sock', mountPath: '/var/run/docker.sock'),
        persistentVolumeClaim(mountPath: '/root/.m2', claimName: 'zcp-jenkins-mvn-repo')
    ]) {
 
    node(label) {
        stage('SOURCE CHECKOUT') {
            def repo = checkout scm
            env.SCM_INFO = repo.inspect()
        }
 
        stage('BUILD MAVEN') {
            container('maven') {
                mavenBuild goal: 'clean package', systemProperties:['maven.repo.local':"/root/.m2/${JOB_NAME}"]
            }
        }
 
        stage('BUILD DOCKER IMAGE') {
            container('docker') {
                dockerCmd.build tag: "${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${VERSION}"
                dockerCmd.push registry: HARBOR_REGISTRY, imageName: DOCKER_IMAGE, imageVersion: VERSION, credentialsId: "HARBOR_CREDENTIALS"
            }
        }
 
        stage('DEPLOY') {
            container('kubectl') {
                kubeCmd.apply file: 'k8s/service.yaml', namespace: K8S_NAMESPACE
                yaml.update file: 'k8s/deploy.yaml', update: ['.spec.template.spec.containers[0].image': "${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${VERSION}"]
                kubeCmd.scale file: 'k8s/deploy.yaml', replicas: '0', namespace: K8S_NAMESPACE
                kubeCmd.apply file: 'k8s/deploy.yaml', namespace: K8S_NAMESPACE, wait: 300
            }
        }
    }
}