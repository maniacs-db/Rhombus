{
    "name" : "pifunctional",
    "replicationClass" : "SimpleStrategy",
    "replicationFactors" : {
        "replication_factor" : 1
    },
    "definitions" : [
        {
            "name": "simple",
            "allowNullPrimaryKeyInserts": true,
            "fields": [
                {"name": "index_1", "type": "varchar"},
                {"name": "index_2", "type": "varchar"},
                {"name": "value", "type": "varchar"}
            ],
            "indexes" : [
                {
                    "key": "index_1",
                    "shardingStrategy": {"type": "ShardingStrategyNone"}
                },
                {
                    "key": "index_2",
                    "shardingStrategy": {"type": "ShardingStrategyNone"}
                }
            ]
        }
    ]
}


