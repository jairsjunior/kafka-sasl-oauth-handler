# CONFLUENT REST PROXY RESOURCE EXTENSION CLASS

## How It Works

When you declare the propertie *kafka.rest.resource.extension.class* in your property class you can handle every call
of Kafka Rest enpoints to do some job like filtering,inject used defined resources, logging or security.
The class defined needs to implement the interface *RestResourceExtension*, that we can find on kafka-rest package.

## Introduce OAuth IMS Authentication at Confluent Rest Proxy

To introduce the IMS authentication at the rest-proxy product, we will use the *RestResourceExtension* implementation to do some
steps and propagate this authentication to our kafka broker, that will be configured to handle OAUTHBEARER conections.
Let's see what steps we will need to handle:

1. Extract the http header `Authorization` that contains the IMS Access Token passed by our client.
2. Detect the type of call made (Producer, Consumer or Administration tasks), because we can had different configurations for each one.
3. Detect if the Client ID had another context connection created and use it or if don't have create it.
4. Create a virtual configuration of the new connection that we handle if it's a new context.
5. Use the specific *AuthenticationCallbackHandler* to inject the Access Token to be propagated to the kafka broker.