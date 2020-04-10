@Library('retort-lib') _
def label = "jenkins-${UUID.randomUUID().toString()}"
 
def USERID = 'admin'
def INTERNAL_REGISTRY = 'pog-dev-registry'
def DOCKER_IMAGE = 'earth1223/hellozcp'
def K8S_NAMESPACE = 'earth1223'
def DEV_VERSION = 'develop'
def PROD_VERSION = 'prod'


podTemplate(label:label,
    serviceAccount: "zcp-system-sa-${USERID}",
    containers: [
        containerTemplate(name: 'maven', image: 'maven:3.5.2-jdk-8-alpine', ttyEnabled: true, command: 'cat'),
        // containerTemplate(name: 'docker', image: 'docker:19-dind', ttyEnabled: true, command: 'dockerd-entrypoint.sh', privileged: true),
        containerTemplate(name: 'buildah', image: 'buildah/buildah', ttyEnabled: true, command: 'cat', privileged: true),
        containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl', ttyEnabled: true, command: 'cat')
    ],
    volumes: [
        persistentVolumeClaim(mountPath: '/root/.m2', claimName: 'zcp-jenkins-mvn-repo'),
        hostPathVolume(hostPath: '/var/lib/containers', mountPath: '/var/lib/containers')
    ]) {
 
    node(label) {
        stage('PRINT VARIABLES') {
            echo '${env.HARBOR_REGISTRY}'
        }
        
        stage('SOURCE CHECKOUT') {
            def repo = checkout scm
            env.SCM_INFO = repo.inspect()
        }
 
        //stage('BUILD MAVEN') {
        //    container('maven') {
        //        mavenBuild goal: 'clean package', systemProperties:['maven.repo.local':"/root/.m2/${JOB_NAME}"]
        //    }
        //}
 
        stage('PULL DOCKER IMAGE') {
            container('buildah') {
                sh 'buildah version'
                sh 'buildah pull ${env.INTERNAL_REGISTRY}/${env.DOCKER_IMAGE}:${DEV_VERSION}'
                //dockerCmd.build tag: "${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
                //dockerCmd.push registry: HARBOR_REGISTRY, imageName: DOCKER_IMAGE, imageVersion: DEV_VERSION, credentialsId: "HARBOR_CREDENTIALS"
            }
        }
     
        stage('SIGN IMAGE') {
            container('buildah') {
                sh 'echo sign image'
            }
        }
     
        stage('ANCHORE EVALUATION') {
            def imageLine = "${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
            writeFile file: 'anchore_images', text: "${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
            anchore name: 'anchore_images', policyBundleId: 'anchore_skt_hcp_bmt'
        }
     
        stage('RETAG DOCKER IMAGE') {
            container('buildah') {
                sh 'buildah tag ${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION} ${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${PROD_VERSION}'
            }
        }
        
        stage('PUSH DOCKER IMAGE') {
            container('buildah') {
                sh 'buildah push ${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${PROD_VERSION}'
            }
        }
 
        stage('DEPLOY') {
            container('kubectl') {
                kubeCmd.apply file: 'k8s/service.yaml', namespace: K8S_NAMESPACE
                yaml.update file: 'k8s/deploy.yaml', update: ['.spec.template.spec.containers[0].image': "${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${VERSION}"]
                def exists = kubeCmd.resourceExists file: 'k8s/deploy.yaml', namespace: K8S_NAMESPACE
                if (exists) {
                    kubeCmd.scale file: 'k8s/deploy.yaml', replicas: '0', namespace: K8S_NAMESPACE
                }
                kubeCmd.apply file: 'k8s/deploy.yaml', namespace: K8S_NAMESPACE, wait: 300
            }
        }
    }
}
