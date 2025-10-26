package contracts.account

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Добавление нового аккаунта"
    request {
        method 'POST'
        url '/accounts'
        headers {
            contentType('multipart/form-data')
        }
        multipart([
                currency: 'USD'
        ])
    }
    response {
        status 302
        headers {
            header('Location', '/')
        }
    }
}
