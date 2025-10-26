package contracts.user

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Изменение пароля — успешный сценарий"

    request {
        method POST()
        url("/user/password")
        headers {
            contentType(applicationJson())
        }
        body([
                password        : "newpass",
                confirm_password : "newpass"
        ])
    }

    response {
        status 200
    }
}
