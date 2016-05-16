revise()
{
    echo -n "\"$1\" + \"$2\": "
    java -jar ./beliefrevisor-1.0.jar "$1" "$2"
}

revise "c and -d" "c and -d"
revise "a and b or (c and -d)" "a and b or (c and -d)"
revise "a and (q then r iff b) or (-c and -d)" "a and (q then r iff b) or (-c and -d)"
