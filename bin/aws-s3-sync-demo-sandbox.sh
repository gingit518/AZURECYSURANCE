#!/bin/bash

aws s3 --profile vrisk sync s3://vrisk-sandbox s3://vrisk-demo

# aws s3 --profile vrisk sync s3://vrisk-sandbox/ ./vrisk-sandbox
