package contracts.cash

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Снятие со счета"
    request {
        method 'POST'
        url '/cash'
        headers {
            contentType('multipart/form-data')
        }
        multipart([
                accountId: 1,
                volume: 50.0,
                action: 'GET'
        ])
    }
    response {
        status 302
        headers {
            header('Location', '/')
        }
    }
}
