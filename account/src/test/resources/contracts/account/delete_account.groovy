package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Удаление аккаунта по ID"

    request {
        method DELETE()
        url("/accounts/1")
    }

    response {
        status 400
    }
}
