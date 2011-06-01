#!/bin/sh
curl -X POST http://localhost:3000/padoca/enqueue \
     -d 'payload={"name":"panguan", "type":"job", "description":"blah blah blah"}' \
     -i