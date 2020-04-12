@Library('retort-lib') _
def label = "jenkins-${UUID.randomUUID().toString()}"
 
def USERID = 'admin'
def INTERNAL_REGISTRY = 'default-route-openshift-image-registry.apps.hcp.skcloud.io'
def DOCKER_IMAGE = 'bmt-workload/ghost'
def K8S_NAMESPACE = 'earth1223'
def DEV_VERSION = 'latest'
def PROD_VERSION = 'prod'
def GPG_KEY = """-----BEGIN PGP PUBLIC KEY BLOCK-----
Version: GnuPG v2.0.22 (GNU/Linux)

mQENBF6TQ5IBCAC+lyu21/G2E/rXQgiJ+rL/4eZV7rVocT7yYFlwr0i0oih7H32+
+HA97vAOJUCWkz3TPh+vyCp9nw5XmteTgWTLyf0WRBeaNpQSZk3leqloWOXP/Q+Y
WRGevE9eMboVUyZEjnNHf2D2voqq/VmVdd88avKFjlPMNDZb7vombJukInOPYHHD
2h7/EPYk7jK5tPuq/Whym4h9bX0LMVJVfcwlCaAWvDCNgeLloKCt5kanyKBE2MsX
CLJZy+6hm41+4c/YvTOKwKxh8bFqBSEDbIPIbI8E+RgcRtJ9ZuoXR5XYdBki/HXU
2FWb4YIYH3//mlEvLzsrsL3Ji4BDu8lC/TYrABEBAAG0I3pvZGlhYzEyayAoVGVz
dCkgPHpvZGlhYzEya0Bzay5jb20+iQE5BBMBAgAjBQJek0OSAhsDBwsJCAcDAgEG
FQgCCQoLBBYCAwECHgECF4AACgkQACBMCPFztBmebAf+JcmhkSR66ayTrCrxoWRq
2OITUY4Khtopbrruz4IicEoydjRdkwPS40LVo012GCieNX4y3Dmy7j9nHsYEfKY9
zPlpU4Z2V1y3VtuUFxbsZyg6ZSJ+dJCmiwES/1MAKHVBSb8MbOKXFCcOZR9Fxvtn
SzqS2BRqjv+9xUV9ttFZ0LOEdW3HovsVMSWNJW/lDqm2o6gd7y5fCzH8Q2Wawvm6
uHBMF59utcQhdMAs6PKg94wDNn2Z97ohrC9Aww4hVWVThD566eUxHY4h07U0BLac
P+Yf6t/1iEXFT2OukqPLtGEvqIDdx28+ofEazBPI90N7Je6+sEOpbWzWBs71QQr9
6rkBDQRek0OSAQgApUydrS5Tt9ASED4y5qtG6DXOMNzDJQwYdYxAJfJe1Gq+06An
zDSRQalE6bUZk6TE66aYjdSw0gNidJ1iKKpViRZJOfGUqCHyBlI/V+eDi9PPquWc
Gf9754UqYlAge1ESdeB75WP1kJHjJ4zn6frYUyJbZCZOFiWavDqp3M89rAXgJ1tm
q0xf+liZWqjfi7V2tPWlDmj8yJKh6neLooKWqNW3CKbB6RC48Sg0peYcO36PMo8I
J81yXV4xa4500FQmetzcP6dr2mGsth/t7ee5mVolnV9R9+Mk1P3+5Wg75OcsaNFy
yaPjhrB8mTGBDJj7jmYpDIr5FI7ZeLzIMGP05wARAQABiQEfBBgBAgAJBQJek0OS
AhsMAAoJEAAgTAjxc7QZPEAH/1HYxhGe3lTeCemxUoAZj+czCEXGNjJySoZgBcWQ
uRTmIn7BxeNK7lCfHFHZIvVVm7qfJW626X5BneSHGkpayQgLk5dMyo9NF8afqqSy
IXqRbtVnYGBUKfz/YYlW6UlfIcbuDjELOxsPsUz9cW1ykIOehfl5NGqH0IY2B8AJ
XLpWIyip0McI0EaeZvIcT+cKUHs4e1okjNJWod240tj0IGU5CmS6aGGbegzTxqrZ
ZUDCym0k9763N5Ot24yggOrcqwE005YeNnm63Sojvi5o/DTrp5I1VKcVpvjhmYGZ
nOpIOAi7/NvboWkEPWXTbvt+bN11O4PLso4uKwM9bYaAzoc=
=Wty4
-----END PGP PUBLIC KEY BLOCK-----"""

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
                sh "buildah bud --format docker --tag ${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION} ."
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
     
        stage('PUSH DOCKER IMAGE') {
            withCredentials([usernamePassword(credentialsId: 'harbor-credentials', passwordVariable: 'HARBOR_PASSWORD', usernameVariable: 'HARBOR_USERNAME')]) {
                container('buildah') {
                    // https://github.com/containers/buildah/blob/master/docs/buildah-login.md
                    sh "notary -s http://harbor-harbor-notary-server.ns-repository:4443"
                    sh "buildah login -u ${HARBOR_USERNAME} -p ${HARBOR_PASSWORD} --tls-verify=false ${HARBOR_REGISTRY}"
                    sh "buildah push --tls-verify=false ${HARBOR_REGISTRY}/${DOCKER_IMAGE}:${PROD_VERSION}"
                }
            }
        }
     
        stage('ANCHORE EVALUATION') {
            def imageLine = "${INTERNAL_REGISTRY}/${DOCKER_IMAGE}:${DEV_VERSION}"
            writeFile file: 'anchore_images', text: imageLine
            anchore name: 'anchore_images'//, policyBundleId: 'anchore_skt_hcp_bmt'
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
