#!/bin/sh

BEARER_TOKEN="pasteYourAccessTokenHereToTest"

# Produce a message using JSON with the value '{ "foo": "bar" }' to the topic test-topic
curl -X POST -H "Content-Type: application/vnd.kafka.json.v2+json" \
      -H "Accept: application/vnd.kafka.v2+json" \
      -H "Authorization: Bearer $BEARER_TOKEN" \
      --data '{"records":[{"value":{"foo":"bar"}}]}' "http://localhost:8082/topics/test-topic"

# Expected output from preceding command
#   {
#    "offsets":[{"partition":0,"offset":0,"error_code":null,"error":null}],"key_schema_id":null,"value_schema_id":null
#   }

# Create a consumer for JSON data, starting at the beginning of the topic's
# log and subscribe to a topic. Then consume some data using the base URL in the first response.
# Finally, close the consumer with a DELETE to make it leave the group and clean up
# its resources.
curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" \
      -H "Authorization: Bearer $BEARER_TOKEN" \
      --data '{"name": "my_consumer_instance", "format": "json", "auto.offset.reset": "earliest"}' \
      http://localhost:8082/consumers/my_json_consumer

# Expected output from preceding command
#  {
#   "instance_id":"my_consumer_instance",
#   "base_uri":"http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance"
#  }

curl -X POST -H "Content-Type: application/vnd.kafka.v2+json" --data '{"topics":["test-topic"]}' \
      -H "Authorization: Bearer $BEARER_TOKEN" \
      http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance/subscription
# No content in response

curl -X GET -H "Accept: application/vnd.kafka.json.v2+json" \
      -H "Authorization: Bearer $BEARER_TOKEN" \
      http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance/records

# Expected output from preceding command
#   [
#    {"key":null,"value":{"foo":"bar"},"partition":0,"offset":0,"topic":"test-topic"}
#   ]

curl -X DELETE -H "Content-Type: application/vnd.kafka.v2+json" \
      -H "Authorization: Bearer $BEARER_TOKEN" \
      http://localhost:8082/consumers/my_json_consumer/instances/my_consumer_instance
# No content in response