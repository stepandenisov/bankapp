package contracts.user

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Изменение информации о пользователе"

    request {
        method POST()
        url("/user/info")
        headers {
            contentType(applicationJson())
        }
        body([
                fullName        : "Test User",
                birthday        : "01.01.1990"
        ])
    }

    response {
        status 200
    }
}
