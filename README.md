# Mongodb output plugin for Embulk

input source중에 지정된 필드를 subdocument로 만들어서 몽고DB로 저장한다

Dumps records to Mongodb with subdocument

## Overview

* **Plugin type**: output
* **Load all or nothing**: no
* **Resume supported**: no
* **Cleanup supported**: no

## Configuration

- **type**: mongodb_nest (string, required)
- **host**: host name (string, required)
- **port**: port number (integer, default: `27017`)
- **database**: database name (string, required)
- **user**: mongodb account (string, required)
- **password**: password (string, required)
- **collection**: collection name (string, required)
- **key**: primary key of collection (string, required)
- **child**: describe subdocument  (object list, default: null)
- **bulk_size**: bulk upsert size at time (integer, default: `10000`)


## Example

* default configuration
```yaml
out:
  type: mongodb_nest
  host: your-host-name
  database: your-database-name
  user: your-account-name
  password: your-password
  collection: your-collection-name
  key: your-key
  child:
  - {name: mychild, field: time}
  - {name: yourchild, field: comment}
  - {name: mychild, field: purchase}
```

* Embulk input csv sample configuration
```yaml
in:
  type: file
  path_prefix: /Users/focuschange/google/program/embulk/try1/csv/sample_
  decoders:
  - {type: gzip}
  parser:
    charset: UTF-8
    newline: LF
    type: csv
    delimiter: ','
    quote: '"'
    escape: '"'
    null_string: 'NULL'
    trim_if_not_quoted: false
    skip_header_lines: 1
    allow_extra_columns: false
    allow_optional_columns: false
    columns:
    - {name: id, type: long}
    - {name: account, type: long}
    - {name: time, type: timestamp, format: '%Y-%m-%d %H:%M:%S'}
    - {name: purchase, type: timestamp, format: '%Y%m%d'}
    - {name: comment, type: string}
out:
  type: mongodb_nest
  host: your-host-name
  database: your-database-name
  user: your-account-name
  password: your-password
  collection: your-collection-name
  key: your-key
  child:
  - {name: mychild, field: time}
  - {name: yourchild, field: comment}
  - {name: mychild, field: purchase}
```


## Release
* 0.1.1 2018-06-05 [bug fix] When the optional 'child' field is not set, error fixed 
* 0.1.0 2018-05-31 first release 


