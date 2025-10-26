package contracts.user

import org.springframework.cloud.contract.spec.Contract

Contract.make {
    description "Регистрация пользователя"
    request {
        method 'POST'
        url '/user/register'
        headers {
            contentType('multipart/form-data')
        }
        multipart([
                username: 'testuser',
                password: 'password',
                confirmPassword: 'password',
                fullName: 'Test User',
                birthday: '1990-01-01'
        ])
    }
    response {
        status 302
        headers {
            header('Location', '/')
        }
    }
}
