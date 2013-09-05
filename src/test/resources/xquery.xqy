xquery version '1.0-ml';

module namespace test = 'http://lds.org/code/testmodule';

import module namespace util = 'http://lds.org/code/util' at '/util.xqy';

declare namespace xh = 'http://www.w3.org/1999/xhtml';

declare option xdmp:mapping "false";

declare variable $test:DATABASE as xs:string := xdmp:database-name(xdmp:database());

(:~
    Function that adds together two sets of integers
    
    @param $a The first set of integers
    @param $b The second set of integers
    
    @return A sequence of integer results for each pair
:)
declare function test:add($a as xs:integer*, $b as xs:integer*)
as element(div)*
{
    for $a as xs:integer at $index in $a
    let $toAdd as xs:integer := $b[$index]
    return
        <div>{
            fn:sum($a, $toAdd)
        }</div>
};

(:~ 
    Gets the article with the specified id
    
    @param $id The ID of the article
    
    @return An article node
:)
declare function test:getArticle($id as xs:unsignedLong?)
as element(article)?
{
    try {
        /article[@id eq $id]
    } catch ($error) {
        if (fn:exists($error/error:format-string)) then
            element article {
                $error/error:format-string
            }
        else ()
    }
};

declare function test:getType($content as element()?)
as xs:string?
{
    typeswitch($content)
    case element(book) return 
        "chapter"
    case element(article) return 
        "article"
    case element(manual) return 
        "manual"
    default return ()
};