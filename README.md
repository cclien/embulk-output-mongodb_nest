# Mongodb output plugin for Embulk

input source중에 지정된 필드를 subdocument로 만들어서 몽고DB로 저장한다

Dumps records to Mongodb with subdocument

## Overview

* **Plugin type**: output
* **Load all or nothing**: no
* **Resume supported**: no
* **Cleanup supported**: no

## Configuration

* **type**: mongodb_nest (string, required)
* **host**: host name (string, required)
* **port**: port number (integer, default: `27017`)
* **database**: database name (string, required)
* **user**: mongodb account (string, required)
* **password**: password (string, required)
* **collection**: collection name (string, required)
* **key**: primary keys of collection (string array, required, **since v0.1.2**)
* **child**: describe subdocument  (object list, default: null)
* **bulk_size**: bulk upsert size at time (integer, default: `10000`)
* **null_value**: set the value to fill if the field value is null (object, optional, **since v0.1.3**)
  - **string** : default string value (string, default: "")
  - **long** : default long value (long, default: 0)
  - **boolean** : default boolean value (boolean, default: false)
  - **double** : default double value (double, default: 0.0)
  - **json** : default json value (object, default: "{}")
  - **timestamp** : default timestamp value (timestamp, default : "1970-01-01T00:00:00.000Z")



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
  key: [your-key1, key2, ...]
  child:
  - {name: mychild, field: time}
  - {name: yourchild, field: comment}
  - {name: mychild, field: purchase}
  null_value:
    string: ""
    long: -1
    json: {key: value}
    boolean: false
    double: 0.0
    timestamp: "1970-01-01T00:00:00.000Z"
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
  key: [your-key, key2]
  child:
  - {name: mychild, field: time}
  - {name: yourchild, field: comment}
  - {name: mychild, field: purchase}
  null_value:
    string: ""
    long: -1
    json: {key: value}
    timestamp: "1990-01-01T00:00:00.000Z"
```


## Release
* 0.1.3 2018-06-25 set the value to fill if the field value is null
* 0.1.2 2018-06-08 The key field has been changed to a list type. The JSON Parser is supported by the input plugin.
* 0.1.1 2018-06-05 [bug fix] Fiexed an error when a child field was entered
* 0.1.0 2018-05-31 first release


