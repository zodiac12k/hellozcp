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
                sh "buildah login -u robot$jenkins -p eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE1ODkyOTg4NTgsImlhdCI6MTU4NjcwNjg1OCwiaXNzIjoiaGFyYm9yLXRva2VuLWRlZmF1bHRJc3N1ZXIiLCJpZCI6NSwicGlkIjozLCJhY2Nlc3MiOlt7IlJlc291cmNlIjoiL3Byb2plY3QvMy9yZXBvc2l0b3J5IiwiQWN0aW9uIjoicHVzaCIsIkVmZmVjdCI6IiJ9XX0.uVNus8kzp4TjX34r74jQGIm4N96SkYiLRaCMHf16CATYQlRPUAXOtzBD5QF-eFfVNvRJcfdqo5mqqdhatoRaeLfwwSONP-sLSb6LflySiF_pApG9vzotp9_-sVl-nayrMmguN618ZuTYuV4q1YPUa6II50e7yKxspYU4YYYVtpoux70VvmbT6AgdyGlDrnHeMv15plorMrDqQtVoBsjEaFIqlNuu8FdAM4yCKSpU7eF-0bX4N-ZfUZfBGgjmFhT87gBXIlE5WZU14qPeb91_3Tak8-aYaX_dkrDYy6Vmr9bvQfD0NicDls0Vv8g2VtV6eacBwB8RSBBgxCM_MhGRAhx79ZaZsGH9c466T82gws2xAfu5z3-7E7T5oCxyzqwuupHJozdD8mMUhCU0AjIAQBv64-SYmCvMZYxV4eFBjlwxGR4SCDqC9pzksatDWCkBhLL3bKESZjsO_FdSvzcpWbWUtzIdaPy0clyCpaSRbtA9ew4MqC8_WBv0xcbTPtAHSkyXrFdRn-wcJBXZAb8LqKpaJ7lqF3ExIllV-hdnzZ_OUidSCOF5rgLktjYGQPnYe5Gx3JG3SzQ4QxLuheVBmd1AQSJd3rQPUjThe_WZ2oeaewqV0sAyI6A4k6IeRwqOKUImhbwl9707c9QTvl4OTMIK6bNVqntTaRVyMj-qR-o --tls-verify=false ${HARBOR_REGISTRY}"
                sh "buildah push --sign-by zodiac12k@sk.com --tls-verify=false ${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${PROD_VERSION}"
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
