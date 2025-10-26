package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Успешная регистрация пользователя"

    request {
        method POST()
        url("/auth/register")
        headers {
            contentType(applicationJson())
        }
        body([
                username        : "new_user",
                password        : "1234",
                confirm_password : "1234",
                full_name        : "New User",
                birthday        : "01.01.1990"
        ])
    }

    response {
        status 201
        body("Пользователь успешно зарегистрирован")
    }
}
