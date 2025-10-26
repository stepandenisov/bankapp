package contracts.transfer

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Перевод на собственный счет"
    request {
        method 'POST'
        url '/transfer/self'
        headers {
            contentType('multipart/form-data')
        }
        multipart([
                fromAccountId: 1,
                toAccountId: 2,
                volume: 50.0
        ])
    }
    response {
        status 302
        headers {
            header('Location', '/')
        }
    }
}
