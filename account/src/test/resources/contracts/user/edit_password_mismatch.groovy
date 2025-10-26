package contracts.user

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Изменение пароля — несовпадение паролей"

    request {
        method POST()
        url("/user/password")
        headers {
            contentType(applicationJson())
        }
        body([
                password        : "1234",
                confirm_password : "abcd"
        ])
    }

    response {
        status 400
        body("Пароли не совпадают")
    }
}
