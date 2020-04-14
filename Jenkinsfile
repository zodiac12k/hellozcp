@Library('retort-lib') _
def label = "jenkins-${UUID.randomUUID().toString()}"
 
def USERID = 'admin'
def DOCKER_IMAGE = 'bmt-workload/ghost'
def K8S_NAMESPACE = 'earth1223'
def DEV_VERSION = 'latest'
def PROD_VERSION = 'prod'

podTemplate(label:label,
    serviceAccount: "zcp-system-sa-${USERID}",
    containers: [
        containerTemplate(name: 'maven', image: 'maven:3.5.2-jdk-8-alpine', ttyEnabled: true, command: 'cat'),
        containerTemplate(name: 'docker', image: 'earth1223/docker:19-dind', ttyEnabled: true, command: 'dockerd-entrypoint.sh', privileged: true, alwaysPullImage: true),
        containerTemplate(name: 'buildah', image: 'quay.io/buildah/stable', ttyEnabled: true, command: 'cat', privileged: true),
        containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl', ttyEnabled: true, command: 'cat'),
        //containerTemplate(name: 'ubuntu', image: 'ubuntu', ttyEnabled: true, command: 'cat')
    ],
    volumes: [
        persistentVolumeClaim(mountPath: '/root/.m2', claimName: 'zcp-jenkins-mvn-repo'),
        hostPathVolume(hostPath: '/var/lib/containers', mountPath: '/var/lib/containers')
    ]) {
 
    node(label) {
        stage('CHECKOUT') {
            def repo = checkout scm
            //env.SCM_INFO = repo.inspect()
        }
 
        //stage('BUILD MAVEN') {
        //    container('maven') {
        //        mavenBuild goal: 'clean package', systemProperties:['maven.repo.local':"/root/.m2/${JOB_NAME}"]
        //    }
        //}
        
        //stage('BUILD DOCKER IMAGE') {
        //    container('buildah') {
        //        sh "buildah version"
        //        sh "buildah bud --format docker --tag ${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION} ."
                //dockerCmd.build tag: "${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
                //dockerCmd.push registry: HARBOR_REGISTRY, imageName: DOCKER_IMAGE, imageVersion: DEV_VERSION, credentialsId: "HARBOR_CREDENTIALS"
        //    }
        //}
 
        stage('PULL DEVELOP IMAGE') {
            withCredentials([usernamePassword(credentialsId: 'internal-registry-credentials', passwordVariable: 'INTERNAL_REGISTRY_PASSWORD', usernameVariable: 'INTERNAL_REGISTRY_USERNAME')]) {
                container('buildah') {
                    // https://github.com/containers/buildah/blob/master/docs/buildah-pull.md
                    sh "buildah pull --creds ${INTERNAL_REGISTRY_USERNAME}:${INTERNAL_REGISTRY_PASSWORD} --tls-verify=false ${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
                    //dockerCmd.build tag: "${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
                    //dockerCmd.push registry: HARBOR_REGISTRY, imageName: DOCKER_IMAGE, imageVersion: DEV_VERSION, credentialsId: "HARBOR_CREDENTIALS"
                }
            }
        }
     
        stage('RETAG DOCKER IMAGE') {
            container('buildah') {
                sh "buildah tag ${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION} ${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${PROD_VERSION}"
            }
        }
        
        stage('PULL IMAGE') {
            withCredentials([usernamePassword(credentialsId: 'internal-registry-credentials', passwordVariable: 'INTERNAL_REGISTRY_PASSWORD', usernameVariable: 'INTERNAL_REGISTRY_USERNAME')]) {
                container('docker') {
                    sh "docker login -u ${INTERNAL_REGISTRY_USERNAME} -p ${INTERNAL_REGISTRY_PASSWORD} ${INTERNAL_REGISTRY}"
                    sh "docker pull ${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
                    sh "docker logout ${INTERNAL_REGISTRY}"
                }
            }
        }
        
        stage('SIGN AND PUSH IMAGE') {
            withCredentials([usernamePassword(credentialsId: 'harbor-credentials', passwordVariable: 'HARBOR_PASSWORD', usernameVariable: 'HARBOR_USERNAME')]) {
                container('docker') {
                    sh "docker login -u ${HARBOR_USERNAME} -p ${HARBOR_PASSWORD} ${HARBOR_REGISTRY}"
                    sh "export DOCKER_CONTENT_TRUST=1"
                    sh "export DOCKER_CONTENT_TRUST_SERVER=${NOTARY_SERVER}"
                    sh "export DOCKER_CONTENT_TRUST_ROOT_PASSPHRASE=${DOCKER_CONTENT_TRUST_ROOT_PASSPHRASE}"
                    sh "export DOCKER_CONTENT_TRUST_REPOSITORY_PASSPHRASE=${DOCKER_CONTENT_TRUST_REPOSITORY_PASSPHRASE}"
                    sh "docker trust key generate ${USERID}"
                    sh "docker trust signer add --key ${USERID}.pub ${USERID} ${HARBOR_REGISTRY}/${DOCKER_IMAGE}"
                    sh "docker tag ${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION} ${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${PROD_VERSION}"
                    sh "docker push ${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${PROD_VERSION}"
                    sh "docker logout ${HARBOR_REGISTRY}"
                    sh "ls ~/.docker/"
                    sh "ls ~/.docker/trust/private"
                    sh "ls ~/.docker/trust/tuf"
                    //dockerCmd.push registry: HARBOR_REGISTRY, imageName: DOCKER_IMAGE, imageVersion: PROD_VERSION, credentialsId: "harbor-credentials"
                }
            }
        }
     
        //stage('PUSH DOCKER IMAGE') {
        //    withCredentials([usernamePassword(credentialsId: 'harbor-credentials', passwordVariable: 'HARBOR_PASSWORD', usernameVariable: 'HARBOR_USERNAME')]) {
        //        container('buildah') {
                    // https://github.com/containers/buildah/blob/master/docs/buildah-push.md
        //            sh "buildah push --creds ${HARBOR_USERNAME}:${HARBOR_PASSWORD} --tls-verify=false ${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${PROD_VERSION}"
        //        }
        //    }
        //}
     
        stage('ANCHORE EVALUATION') {
            def imageLine = "${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
            writeFile file: 'anchore_images', text: imageLine
            anchore name: 'anchore_images', engineRetries: '3000'//, policyBundleId: 'anchore_skt_hcp_bmt'
        }

        stage('DEPLOY') {
            container('kubectl') {
                cluster("skt-bmt/ns-swing,eks-bmt-1/ns-swing") {
                    echo "Work in ${it.cluster}"
                    sh "kubectl get po -n ${it.namespace}"
                    kubeCmd.apply file: 'k8s/service.yaml', namespace: K8S_NAMESPACE
                    yaml.update file: 'k8s/deploy.yaml', update: ['.spec.template.spec.containers[0].image': "${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${PROD_VERSION}"]
                    def exists = kubeCmd.resourceExists file: 'k8s/deploy.yaml', namespace: K8S_NAMESPACE
                    if (exists) {
                        kubeCmd.scale file: 'k8s/deploy.yaml', replicas: '0', namespace: K8S_NAMESPACE
                    }
                    kubeCmd.apply file: 'k8s/deploy.yaml', namespace: K8S_NAMESPACE, wait: 300
                }
            }
        }
    }
}
