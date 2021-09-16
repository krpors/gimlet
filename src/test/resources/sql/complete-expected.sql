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
    some_table.id = ?
and some_table.name = ?
and some_table.other = ?
and some_table.bla = ?
and some_table.somenum = ?
and some_other_table = ? -- same id here
