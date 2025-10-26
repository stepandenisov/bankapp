package contracts.user

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Удаление текущего пользователя"

    request {
        method DELETE()
        url("/user")
    }

    response {
        status 200
    }
}
