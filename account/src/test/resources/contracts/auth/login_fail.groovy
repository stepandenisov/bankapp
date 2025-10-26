package contracts.auth

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Ошибка авторизации при неверном пароле"

    request {
        method POST()
        url("/auth/login")
        headers {
            contentType(applicationJson())
        }
        body([
                username: "test",
                password: "wrong"
        ])
    }

    response {
        status 401
        body("Неверное имя пользователя или пароль")
    }
}
