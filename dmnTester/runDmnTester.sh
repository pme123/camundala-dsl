docker run \
  --name camunda-dmn-tester \
   --rm \
   -it \
   -e TESTER_CONFIG_PATHS="/dmnConfigs" \
   -v $(pwd)/dmnConfigs:/opt/docker/dmnConfigs \
   -v $(pwd)/../examples:/opt/docker/examples \
   -p 8883:8883 \
   pame/camunda-dmn-tester