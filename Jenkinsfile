@Library('retort-lib') _
def label = "jenkins-${UUID.randomUUID().toString()}"
 
def USERID = 'admin'
def INTERNAL_REGISTRY = 'default-route-openshift-image-registry.apps.hcp.skcloud.io'
def DOCKER_IMAGE = 'bmt-workload/ghost'
def K8S_NAMESPACE = 'earth1223'
def DEV_VERSION = 'latest'
def PROD_VERSION = 'prod'


podTemplate(label:label,
    serviceAccount: "zcp-system-sa-${USERID}",
    containers: [
        containerTemplate(name: 'maven', image: 'maven:3.5.2-jdk-8-alpine', ttyEnabled: true, command: 'cat'),
        //containerTemplate(name: 'docker', image: 'docker:17-dind', ttyEnabled: true, command: 'dockerd-entrypoint.sh', privileged: true),
        containerTemplate(name: 'buildah', image: 'quay.io/buildah/stable', ttyEnabled: true, command: 'cat', privileged: true),
        containerTemplate(name: 'kubectl', image: 'lachlanevenson/k8s-kubectl', ttyEnabled: true, command: 'cat')
    ],
    volumes: [
        persistentVolumeClaim(mountPath: '/root/.m2', claimName: 'zcp-jenkins-mvn-repo'),
        hostPathVolume(hostPath: '/var/lib/containers', mountPath: '/var/lib/containers')
    ]) {
 
    node(label) {
        stage('SOURCE CHECKOUT') {
            def repo = checkout scm
            //env.SCM_INFO = repo.inspect()
        }
 
        stage('BUILD MAVEN') {
            container('maven') {
                mavenBuild goal: 'clean package', systemProperties:['maven.repo.local':"/root/.m2/${JOB_NAME}"]
            }
        }
        
        stage('BUILD DOCKER IMAGE') {
            container('buildah') {
                sh "buildah version"
                sh "buildah bud --tag ${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION} ."
                //dockerCmd.build tag: "${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
                //dockerCmd.push registry: HARBOR_REGISTRY, imageName: DOCKER_IMAGE, imageVersion: DEV_VERSION, credentialsId: "HARBOR_CREDENTIALS"
            }
        }
 
        //stage('PULL DEVELOP IMAGE') {
        //    container('buildah') {
                // https://github.com/containers/buildah/blob/master/docs/buildah-login.md
        //        sh "buildah login -u cluster-admin -p 9SIaplD8KwORfNWw63o4eRWvvMm5gYMfU1f-UMbZ5Wg --tls-verify=false ${INTERNAL_REGISTRY}"
        //        sh "buildah pull --tls-verify=false ${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
                //dockerCmd.build tag: "${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
                //dockerCmd.push registry: HARBOR_REGISTRY, imageName: DOCKER_IMAGE, imageVersion: DEV_VERSION, credentialsId: "HARBOR_CREDENTIALS"
        //    }
        //}
     
        stage('SIGN IMAGE') {
            container('buildah') {
                sh "cat /etc/containers/registries.conf"
                sh "cat /etc/containers/policy.json"
            }
        }
     
        stage('RETAG DOCKER IMAGE') {
            container('buildah') {
                sh "buildah tag ${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION} ${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${PROD_VERSION}"
            }
        }
     
        //stage('ANCHORE EVALUATION') {
        //    def imageLine = "${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
        //    writeFile file: 'anchore_images', text: imageLine
        //    anchore name: 'anchore_images'//, policyBundleId: 'anchore_skt_hcp_bmt'
        //}
     
        stage('PUSH DOCKER IMAGE') {
            container('buildah') {
                // https://github.com/containers/buildah/blob/master/docs/buildah-login.md
                sh "buildah login -u admin -p !Cloudev00 --tls-verify=false ${HARBOR_REGISTRY}"
                sh "buildah push --sign-by zodiac12k@sk.com ${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${PROD_VERSION}"
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
