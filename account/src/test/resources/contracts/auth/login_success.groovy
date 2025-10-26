package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Успешная авторизация пользователя"

    request {
        method POST()
        url("/auth/login")
        headers {
            contentType(applicationJson())
        }
        body([
                username: "test",
                password: "password"
        ])
    }

    response {
        status 200
        headers {
            contentType(applicationJson())
        }
        body([
                accessToken: $(regex('[A-Za-z0-9\\-_.]+'))
        ])
    }
}
