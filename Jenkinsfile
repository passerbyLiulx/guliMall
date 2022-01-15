#!groovy
def modelName = "${model_name}"

def sp = modelName.split("/")
def serviceName = sp[sp.length-1]
// git
// git凭证id
def id = ""
def gitUrl = "https://codeup.teambition.com/teleinfo/sfbn/sfbn-server.git"
def branch = "${branch}"

// docker
def dockerUrl = "39.99.132.122:9001"
def dockerUserName = "admin"
def dockerPassword = "T1q2w3e4r"
def dockerNameSpace = "sfbn"
def imageName = "${serviceName}:${branch}"

pipeline {

    agent any

    stages {
        stage('拉取git代码') {
            steps {
                echo "部署模块：${serviceName},git分支：${branch},正在拉取代码..."
                checkout([$class: 'GitSCM', branches: [[name: branch]], extensions: [],
                           userRemoteConfigs: [[credentialsId: id,
                           url: gitUrl]]])
                echo "拉取代码完成..."
            }
        }

        stage('编译代码') {
            steps {
                echo "开始进行编译..."
                sh " mvn clean -Dmaven.test.skip=true package -pl ${modelName} -am "
                echo "编译操作结束..."
            }
        }

        stage('打包docker') {
            steps {
                echo "开始docker 操作..."
                // 构建
                sh " docker build -t ${dockerUrl}/${dockerNameSpace}/${imageName} ${modelName}"

                // 登录
                sh "docker login -u ${dockerUserName} -p ${dockerPassword} ${dockerUrl}"

                // push
                sh "docker push ${dockerUrl}/${dockerNameSpace}/${imageName}"

                // 删除
                sh " docker rmi ${dockerUrl}/${dockerNameSpace}/${imageName}"
                echo "开始docker 操作完成..."
            }
        }

         stage('连接ssh, 执行 docker compose') {
             steps {
                 echo "开始执行 ssh..."
                 sshPublisher(publishers: [sshPublisherDesc(configName: "${branch}", transfers: [sshTransfer(cleanRemote: false, excludes: '', execCommand:

                 "cd /opt/dapp/${dockerNameSpace}/;" +
                 "docker-compose stop ${serviceName};" +
                 "docker-compose rm -f ${serviceName};" +
                 "docker rmi ${dockerUrl}/${dockerNameSpace}/${imageName};" +
                 "docker pull ${dockerUrl}/${dockerNameSpace}/${imageName};" +
                 "docker-compose up -d ${serviceName}",
                         execTimeout: 720000, flatten: false, makeEmptyDirs: false, noDefaultExcludes: false,
                         patternSeparator: '[, ]+', remoteDirectory: '', remoteDirectorySDF: false, removePrefix: '',
                         sourceFiles: '')], usePromotionTimestamp: false, useWorkspaceInPromotion: false, verbose: false)])
                 echo "ssh 执行结束..."
             }
         }
    }
}