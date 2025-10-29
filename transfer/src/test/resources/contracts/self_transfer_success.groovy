package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Self transfer success"

    request {
        method POST()
        url("/transfer/self")
        headers {
            contentType(applicationJson())
        }
        body(
                fromAccountId: 1L,
                toAccountId:2L,
                amount: 50.0
        )
    }

    response {
        status 200
    }
}
