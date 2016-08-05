#!/usr/bin/env bash

echo "Downloading LetsEncrypt.org certificate..."
wget https://letsencrypt.org/certs/lets-encrypt-x3-cross-signed.der -O lets-encrypt-x3-cross-signed.der
if [ $? -eq 0 ]; then
    echo "Done"
    echo
    echo "Adding certificate to keystore in JAVA_HOME=$JAVA_HOME/jre/lib/security/cacerts"
    sudo keytool -trustcacerts -keystore $JAVA_HOME/jre/lib/security/cacerts -storepass changeit -noprompt -importcert -alias lets-encrypt-x3-cross-signed -file lets-encrypt-x3-cross-signed.der
    if [ $? -eq 0 ]; then
        echo
        echo "Success."
        exit
    else
        echo
        echo "Unable to install certificate in keystore"
    fi
    rm -f lets-encrypt-x3-cross-signed.der
    exit
else
    echo "Unable to download certificate"
fi