package contracts.transfer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Внешний перевод"
    request {
        method 'POST'
        url '/transfer/external'
        headers {
            contentType('multipart/form-data')
        }
        multipart([
                userId: 1,
                fromAccountId: 1,
                toCurrency: 'RUB',
                amount: 75.0
        ])
    }
    response {
        status 302
        headers {
            header('Location', '/')
        }
    }
}