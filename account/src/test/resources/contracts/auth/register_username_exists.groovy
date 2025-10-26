package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Регистрация с уже существующим логином"

    request {
        method POST()
        url("/auth/register")
        headers {
            contentType(applicationJson())
        }
        body([
                username        : "test",
                password        : "1234",
                confirm_password : "1234",
                full_name        : "Test User",
                birthday        : "01.01.1990"
        ])
    }

    response {
        status 400
        body("Пользователь с таким логином уже существует")
    }
}
