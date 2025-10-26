package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "External transfer success"

    request {
        method POST()
        url("/transfer/external")
        headers {
            contentType(applicationJson())
        }
        body(
                fromAccount: "2",
                toAccount: "1",
                amount: 100.0
        )
    }

    response {
        status 200
    }
}
