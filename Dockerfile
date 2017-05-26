FROM    maven:alpine

COPY    . /usr/src/app

WORKDIR /usr/src/app

CMD     ["./mvnw", "install"]
