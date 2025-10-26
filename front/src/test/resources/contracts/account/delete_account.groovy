package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Удаление аккаунта"
    request {
        method 'POST'
        url '/accounts/1/delete'
    }
    response {
        status 302
        headers {
            header('Location', '/')
        }
    }
}
