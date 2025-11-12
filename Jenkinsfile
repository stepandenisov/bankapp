pipeline {
    agent any

    environment {
        MINIKUBE_DRIVER = "hyperv"
        NAMESPACE = "bankapp"
    }

    stages {
        stage('Start Minikube') {
            steps {
                echo "Starting Minikube"
                sh '''
                    minikube start --driver=${MINIKUBE_DRIVER} --memory=4000 --cpus=4
                '''
            }
        }

        stage('Configure Docker inside Minikube') {
            steps {
                echo "Configuring Docker env for Minikube"
                sh '''
                    eval $(minikube -p minikube docker-env)
                    docker info
                '''
            }
        }

        stage('Build Maven project') {
            steps {
                echo "Building with Maven"
                sh '''
                    mvn clean install -DskipTests
                '''
            }
        }

        stage('Build Docker images') {
            steps {
                echo "Building Docker images"
                sh '''
                    eval $(minikube -p minikube docker-env)

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
                '''
            }
        }

        stage('Prepare Kubernetes') {
            steps {
                echo "Creating namespace, secrets, and configmaps"
                sh '''
                    kubectl create namespace ${NAMESPACE} --dry-run=client -o yaml | kubectl apply -f -

                    kubectl create secret generic db-secret -n ${NAMESPACE} \
                        --from-literal=POSTGRES_USER=postgres \
                        --from-literal=POSTGRES_PASSWORD=1 \
                        --from-literal=POSTGRES_DB=yandex \
                        --dry-run=client -o yaml | kubectl apply -f -

                    kubectl create configmap keycloak-realm \
                        --from-file=realm-export.json=./keycloak.json -n ${NAMESPACE} \
                        --dry-run=client -o yaml | kubectl apply -f -
                '''
            }
        }

        stage('Fix CoreDNS') {
            steps {
                echo "Fixing CoreDNS DNS forwarding"
                sh '''
                    kubectl -n kube-system get configmap coredns -o yaml > coredns.yaml
                    sed -i '/forward \./,+2d' coredns.yaml
                    sed -i '/max_concurrent/a\\        forward . 8.8.8.8 1.1.1.1 {\\n           max_concurrent 1000\\n        }' coredns.yaml
                    kubectl -n kube-system apply -f coredns.yaml
                    kubectl -n kube-system rollout restart deployment coredns
                '''
            }
        }

        stage('Deploy with Helm') {
            steps {
                echo "Deploying all microservices with Helm"
                sh '''
                    helm upgrade --install config-server -f ./helm/bankapp/values-config-server.yaml ./helm/bankapp
                    helm upgrade --install keycloak -f ./helm/bankapp/values-keycloak.yaml ./helm/bankapp
                    helm upgrade --install postgres -f ./helm/bankapp/values-postgres.yaml ./helm/bankapp
                    helm upgrade --install account -f ./helm/bankapp/values-account.yaml ./helm/bankapp
                    helm upgrade --install front -f ./helm/bankapp/values-front.yaml ./helm/bankapp
                    helm upgrade --install blocker -f ./helm/bankapp/values-blocker.yaml ./helm/bankapp
                    helm upgrade --install cash -f ./helm/bankapp/values-cash.yaml ./helm/bankapp
                    helm upgrade --install eureka -f ./helm/bankapp/values-eureka.yaml ./helm/bankapp
                    helm upgrade --install exchange -f ./helm/bankapp/values-exchange.yaml ./helm/bankapp
                    helm upgrade --install exchange-generator -f ./helm/bankapp/values-exchange-generator.yaml ./helm/bankapp
                    helm upgrade --install gateway -f ./helm/bankapp/values-gateway.yaml ./helm/bankapp
                    helm upgrade --install notification -f ./helm/bankapp/values-notification.yaml ./helm/bankapp
                    helm upgrade --install transfer -f ./helm/bankapp/values-transfer.yaml ./helm/bankapp
                '''
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
