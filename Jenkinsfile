pipeline {
    agent any

    environment {
        MINIKUBE_DRIVER = "hyperv"
        NAMESPACE = "bankapp"
    }

    stages {
        stage('Setup') {
            steps {
                script {
                    run = { cmd -> if (isUnix()) sh cmd else bat cmd }
                }
            }
        }

        stage('Build Maven project') {
            steps {
                script {
                    run("mvn clean install -DskipTests")
                }
            }
        }

        stage('Build Docker images') {
            steps {
                script {
                    if (isUnix()) {
                        run("""
                            eval \$(minikube -p minikube docker-env)
                            for svc in config-server eureka gateway account blocker cash exchange exchange-generator transfer front notifications; do
                                docker build -t bankapp-\$svc:local -f \$svc/Dockerfile .
                            done
                        """)
                    } else {
                        run("""
                            for /f "tokens=*" %%i in ('minikube -p minikube docker-env --shell cmd') do %%i
                            docker build -t bankapp-config-server:local -f config-server/Dockerfile .
                            docker build -t bankapp-eureka:local -f eureka/Dockerfile .
                            docker build -t bankapp-gateway:local -f gateway/Dockerfile .
                            docker build -t bankapp-account:local -f account/Dockerfile .
                            docker build -t bankapp-blocker:local -f blocker/Dockerfile .
                            docker build -t bankapp-cash:local -f cash/Dockerfile .
                            docker build -t bankapp-exchange:local -f exchange/Dockerfile .
                            docker build -t bankapp-exchange-generator:local -f exchange-generator/Dockerfile .
                            docker build -t bankapp-transfer:local -f transfer/Dockerfile .
                            docker build -t bankapp-front:local -f front/Dockerfile .
                            docker build -t bankapp-notification:local -f notifications/Dockerfile .
                        """)
                    }
                }
            }
        }

        stage('Prepare Kubernetes') {
            steps {
                script {
                    run("""
                        kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -
                        kubectl create secret generic db-secret -n ${NAMESPACE} \
                            --from-literal=POSTGRES_USER=postgres \
                            --from-literal=POSTGRES_PASSWORD=1 \
                            --from-literal=POSTGRES_DB=yandex \
                            --dry-run=client -o yaml | kubectl apply -f -
                        kubectl create configmap keycloak-realm \
                            --from-file=realm-export.json=./keycloak.json -n ${NAMESPACE} \
                            --dry-run=client -o yaml | kubectl apply -f -
                    """)
                }
            }
        }

        stage('Fix CoreDNS') {
            steps {
                script {
                    run("""
                        kubectl -n kube-system get configmap coredns -o yaml > coredns.yaml
                        sed -i '/forward \\\\./,+2d' coredns.yaml
                        sed -i '/max_concurrent/a\\\\        forward . 8.8.8.8 1.1.1.1 {\\\\n           max_concurrent 1000\\\\n        }' coredns.yaml
                        kubectl -n kube-system apply -f coredns.yaml
                        kubectl -n kube-system rollout restart deployment coredns
                    """)
                }
            }
        }

        stage('Kafka') {
            steps {
                script {
                    run("""
                        helm upgrade --install kafka -n bankapp -f ./helm/kafka/values.yaml ./helm/kafka
                        kubectl rollout status statefulset/kafka -n bankapp --timeout=120s
                    """)
                }
            }
        }


        stage('Deploy with Helm') {
            steps {
                script {
                    run("""
                        helm upgrade --install config-server -f ./helm/bankapp/values-config-server.yaml ./helm/bankapp
                        helm upgrade --install keycloak -f ./helm/bankapp/values-keycloak.yaml ./helm/bankapp
                        helm upgrade --install postgres -f ./helm/bankapp/values-postgres.yaml ./helm/bankapp
                        helm upgrade --install eureka -f ./helm/bankapp/values-eureka.yaml ./helm/bankapp
                        helm upgrade --install account -f ./helm/bankapp/values-account.yaml ./helm/bankapp
                        helm upgrade --install front -f ./helm/bankapp/values-front.yaml ./helm/bankapp
                    """)
                }
            }
        }
    }

    post {
        success {
            echo "Deployment completed successfully!"
        }
        failure {
            echo "Build or deployment failed!"
        }
    }
}
