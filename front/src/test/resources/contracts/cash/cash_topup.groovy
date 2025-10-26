package contracts.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Пополнение счета"
    request {
        method 'POST'
        url '/cash'
        headers {
            contentType('multipart/form-data')
        }
        multipart([
                accountId: 1,
                volume: 100.0,
                action: 'PUT'
        ])
    }
    response {
        status 302
        headers {
            header('Location', '/')
        }
    }
}
