package contracts

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Вернет false если не подозрительная операция"
    request {
        method GET()
        url("/")
    }
    response {
        status 200
        body(false)
    }
}
