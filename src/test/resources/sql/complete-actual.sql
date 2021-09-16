-- This file contains a complete SQL with all possible variations to
-- test the parsing.

/*
This is some multiline comment. It's not officially supported in regular SQL,
but we apparently support it anyway.
*/

-- Actual query
select
id,
name::varchar -- test casting here
from some_table
where
    some_table.id = :id
and some_table.name = :name[STRING]
and some_table.other = :other[DATE]
and some_table.bla = :bla[DATETIME]
and some_table.somenum = :num[NUMBER]
and some_other_table = :id -- same id here
