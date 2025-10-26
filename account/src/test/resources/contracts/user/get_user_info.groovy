package contracts.user

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Получение информации о текущем пользователе"

    request {
        method GET()
        url("/user")
        headers {
            accept(applicationJson())
        }
    }

    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
                username: "test",
                fullName: "Test User"
        ])
    }
}
