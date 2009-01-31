#!/usr/bin/env python
# -*- coding: utf-8 -*-


class ExpressionTokenizer(ExpressionTokenizerBase):
    """ generated source for Test

    """
    reversedArray = [()

    def __init__(self, value):
        self.value = value.trim()
        self.end = len(self.value)
        parse()
        self.expression = self.right(self.tokens.iterator())

    def rightEnd(self, item, privious):
        type = item.getType()
        priviousType = privious.getType()
        return False

    def right(self, tokens):
        rightStack = LinkedList()
        rightStack.push(ArrayList())
        buffer = LinkedList()
        while tokens.hasNext():
            item = tokens.next()
            if isinstance(item, (OperatorToken)):
                op = item
                if buffer.isEmpty():
                    buffer.push(op)
                else:
                    if (item.getType() == ExpressionToken.BRACKET_BEGIN):
                        buffer.push(op)
                    else:
                        if (item.getType() == ExpressionToken.BRACKET_END):
                            while True:
                                operator = buffer.pop()
                                if (operator.getType() == ExpressionToken.BRACKET_BEGIN):
                                    break
                                self.addOperator(rightStack, operator)
                        else:
                            while not buffer.isEmpty() and self.rightEnd(op, buffer.getFirst()):
                                operator = buffer.pop()
                                self.addOperator(rightStack, operator)
                            buffer.push(op)
            else:
                self.addToken(rightStack, item)
        while not buffer.isEmpty():
            operator = buffer.pop()
            self.addOperator(rightStack, operator)
        return rightStack.getFirst()

    def addOperator(self, rightStack, operator):
        if operator.getType() == ExpressionToken.OP_QUESTION_SELECT:
            children = rightStack.pop()
            list = rightStack.getFirst()
            token = list[len(list] - 1)
            token.setChildren(self.toArray(children))
        self.addToken(rightStack, operator)

    def addToken(self, rightStack, token):
        list = rightStack.getFirst()
        if isinstance(token, (LazyToken)):
            rightStack.push(ArrayList())
        list.add(token)

    def get_toArray(self):
        if self.reversedArray is None:
            self.reversedArray = self.toArray(expression)
        return self.reversedArray

    def set_toArray(self, list):
        expression = [ExpressionToken() for __idx0 in range(len(list))]
        i = expression.length - 1
        iterator = list.iterator()
        while iterator.hasNext():
            expressionToken = iterator.next()
            expression[i -= 1] = expressionToken
        return expression

    toArray = property(get_toArray, set_toArray)
class ExpressionTokenizer(object):
    """ generated source for TestBase

    """
    STATUS_BEGIN = -100
    STATUS_EXPRESSION = -101
    STATUS_OPERATOR = -102
    PRIORITY_MAP = HashMap()
    OP_TYPE_MAP = HashMap()
    TYPE_OP_MAP = HashMap()
    OPS = [()

    @classmethod
    def addOperator(cls, type, priopity, key, ops):
        cls.PRIORITY_MAP.put(type, priopity)
        if key is not None:
            cls.TYPE_OP_MAP.put(type, key)
            if cls.OP_TYPE_MAP.containsKey(key):
                cls.OP_TYPE_MAP.put(key, -1)
            else:
                cls.OP_TYPE_MAP.put(key, type)
            if len((key) == 1):
                if (",:[{}]".indexOf(key.charAt(0)) == -1):
                    cls.ops.add(key)
            else:
                if len((key) == 2):
                    cls.ops.add(0, key)

    @classmethod
    def getOperator(cls, type):
        return cls.TYPE_OP_MAP[type]

    ops = ArrayList()
    ops.add("[")
    ops.add("{")
    ops.add("]")
    ops.add("}")
    addOperator(ExpressionToken.BRACKET_BEGIN, Integer.MIN_VALUE, "(", ops)
    addOperator(ExpressionToken.BRACKET_END, Integer.MIN_VALUE, ")", ops)
    addOperator(ExpressionToken.OP_GET_PROP, 12, ".", ops)
    addOperator(ExpressionToken.OP_GET_METHOD, 12, None, ops)
    addOperator(ExpressionToken.OP_GET_STATIC_METHOD, 12, None, ops)
    addOperator(ExpressionToken.OP_INVOKE_METHOD, 12, None, ops)
    addOperator(ExpressionToken.VALUE_NEW_LIST, 12, "[", ops)
    addOperator(ExpressionToken.VALUE_NEW_MAP, 12, "{", ops)
    addOperator(ExpressionToken.OP_NOT, 8, "!", ops)
    addOperator(ExpressionToken.OP_POS, 8, "+", ops)
    addOperator(ExpressionToken.OP_NEG, 8, "-", ops)
    addOperator(ExpressionToken.OP_MUL, 4, "*", ops)
    addOperator(ExpressionToken.OP_DIV, 4, "/", ops)
    addOperator(ExpressionToken.OP_MOD, 4, "%", ops)
    addOperator(ExpressionToken.OP_ADD, 1, "+", ops)
    addOperator(ExpressionToken.OP_SUB, 1, "-", ops)
    addOperator(ExpressionToken.OP_LT, 0, "<", ops)
    addOperator(ExpressionToken.OP_GT, 0, ">", ops)
    addOperator(ExpressionToken.OP_LTEQ, 0, "<=", ops)
    addOperator(ExpressionToken.OP_GTEQ, 0, ">=", ops)
    addOperator(ExpressionToken.OP_EQ, 0, "==", ops)
    addOperator(ExpressionToken.OP_NOTEQ, 0, "!=", ops)
    addOperator(ExpressionToken.OP_AND, -1, "&&", ops)
    addOperator(ExpressionToken.OP_OR, -2, "||", ops)
    addOperator(ExpressionToken.OP_QUESTION, -4, "?", ops)
    addOperator(ExpressionToken.OP_QUESTION_SELECT, -4, ":", ops)
    addOperator(ExpressionToken.OP_MAP_PUSH, -7, ":", ops)
    addOperator(ExpressionToken.OP_PARAM_JOIN, -8, ",", ops)
    OPS = ops.toArray([String() for __idx0 in range(len(ops))])
    operatorMap = HashMap()
    CONSTAINS_MAP = HashMap()
    CONSTAINS_MAP.put("true", createToken(ExpressionToken.VALUE_CONSTANTS, Boolean.TRUE))
    CONSTAINS_MAP.put("false", createToken(ExpressionToken.VALUE_CONSTANTS, Boolean.FALSE))
    CONSTAINS_MAP.put("null", createToken(ExpressionToken.VALUE_CONSTANTS, None))

    @classmethod
    def createToken(cls, type, value):
        if type == ExpressionToken.VALUE_VAR:
            return VarToken(cls.value)
        elif type == ExpressionToken.VALUE_NEW_LIST:
            return ValueToken(type, cls.value)
        elif type == ExpressionToken.VALUE_LAZY:
            return LazyToken()
        elif type == ExpressionToken.OP_MAP_PUSH:
            return OperatorToken(type, cls.value)
        else:
            token = cls.operatorMap[type]
            if token is None:
                pass
            return token

    value = ""
    start = 0
    end = 0
    status = STATUS_BEGIN
    previousType = STATUS_BEGIN
    tokens = ArrayList()
    expression = List()

    def parse(self):
        self.skipSpace(0)
        while self.start < self.end:
            c = self.value.charAt(self.start)
            if "c == '\"' || c == '\''" is not None:
                text = self.findString()
                self.addKeyOrObject(text, False)
            else:
                if "c >= '0' && c <= '9'" is not None:
                    number = self.findNumber()
                    self.addKeyOrObject(number, False)
                else:
                    if Character.isJavaIdentifierStart(c):
                        id = self.findId()
                        constains = self.CONSTAINS_MAP[id]
                        if constains is None:
                            self.skipSpace(0)
                            if (self.previousType == ExpressionToken.OP_GET_PROP):
                                self.addToken(self.createToken(ExpressionToken.VALUE_CONSTANTS, id))
                            else:
                                self.addKeyOrObject(id, True)
                        else:
                            self.addToken(constains)
                    else:
                        op = self.findOperator()
                        self.parseOperator(op)
                        if op is None:
                            raise ExpressionSyntaxException("el error:" + self.value + "@" + self.start)
                                pass
                            ExpressionSyntaxException("el error:" + self.value + "@" + self.start)
            self.skipSpace(0)

    def isMapMethod(self):
        i = len(self.tokens) - 1
        depth = 0
        ## for-while
        while i >= 0:
            token = self.tokens[i]
            type = token.getType()
            if (depth == 0):
                if (type == ExpressionToken.OP_MAP_PUSH) or (type == ExpressionToken.VALUE_NEW_MAP):
                    return True
                else:
                    if (type == ExpressionToken.OP_PARAM_JOIN):
                        return False
            if (type == ExpressionToken.BRACKET_BEGIN):
                depth -= 1
            else:
                if (type == ExpressionToken.BRACKET_END):
                    depth += 1
            i -= 1
        return False

    def findOperator(self):
        if self.value.charAt(self.start) == ',':
            self.start += 1
            return ","
        elif self.value.charAt(self.start) == ':':
            self.start += 1
            return ":"
        elif self.value.charAt(self.start) == '[':
            self.start += 1
            return "["
        elif self.value.charAt(self.start) == '{':
            self.start += 1
            return "{"
        elif self.value.charAt(self.start) == ']':
            self.start += 1
            return "]"
        elif self.value.charAt(self.start) == '}':
            self.start += 1
            return "}"
        ## for-while
        i = 0
        while i < self.OPS.length:
            op = self.OPS[i]
            if self.value.startsWith(op, self.start):
                self.start += len(op)
                return op
            i += 1
        return

    def parseOperator(self, op):
        if len((op) == 1):
            if op.charAt(0) == '(':
                if (self.status == self.STATUS_EXPRESSION):
                    self.insertAndReturnIsStatic()
                    self.addToken(self.createToken(ExpressionToken.OP_INVOKE_METHOD, None))
                    if self.skipSpace(')'):
                        self.addToken(self.createToken(ExpressionToken.VALUE_CONSTANTS, Collections.EMPTY_LIST))
                    else:
                        self.addList()
                else:
                    self.addToken(self.createToken(ExpressionToken.BRACKET_BEGIN, None))
            elif op.charAt(0) == '[':
                if (self.status == self.STATUS_BEGIN) or (self.status == self.STATUS_OPERATOR):
                    self.addList()
                else:
                    if (self.status == self.STATUS_EXPRESSION):
                        self.addToken(self.createToken(ExpressionToken.OP_GET_PROP, None))
                        self.addToken(self.createToken(ExpressionToken.BRACKET_BEGIN, None))
                    else:
                        raise ExpressionSyntaxException("el error:" + self.value + "@" + self.start)
                            pass
                        ExpressionSyntaxException("el error:" + self.value + "@" + self.start)
            elif op.charAt(0) == '{':
                self.addMap()
            elif op.charAt(0) == ')':
                self.addToken(self.createToken(ExpressionToken.BRACKET_END, None))
            elif op.charAt(0) == '+':
                self.addToken(self.createToken(ExpressionToken.OP_POS if (self.status == self.STATUS_OPERATOR) else ExpressionToken.OP_ADD, None))
            elif op.charAt(0) == '-':
                self.addToken(self.createToken(ExpressionToken.OP_NEG if (self.status == self.STATUS_OPERATOR) else ExpressionToken.OP_SUB, None))
            elif op.charAt(0) == '?':
                self.addToken(self.createToken(ExpressionToken.OP_QUESTION, None))
                self.addToken(LazyToken())
            elif op.charAt(0) == ':':
                self.addToken(self.createToken(ExpressionToken.OP_QUESTION_SELECT, None))
                self.addToken(LazyToken())
            elif op.charAt(0) == ',':
                if not self.isMapMethod():
                    self.addToken(self.createToken(ExpressionToken.OP_PARAM_JOIN, None))
            elif op.charAt(0) == '/':
                next = self.value.charAt(self.start)
                if (next == '/'):
                    end1 = self.value.indexOf('\n', self.start)
                    end2 = self.value.indexOf('\r', self.start)
                    cend = Math.min(end1, end2)
                    if cend < 0:
                        cend = Math.max(end1, end2)
                    if cend > 0:
                        self.start = cend
                    else:
                        self.start = self.end
                else:
                    if (next == '*'):
                        cend = self.value.indexOf("*/", self.start)
                        if cend > 0:
                            self.start = cend + 2
                        else:
                            raise ExpressionSyntaxException("el error:" + self.value + "@" + self.start)
                                pass
                            ExpressionSyntaxException("el error:" + self.value + "@" + self.start)
            else:
                self.addToken(self.createToken(self.OP_TYPE_MAP[op], None))
        else:
            if op == "||":
                self.addToken(self.createToken(ExpressionToken.OP_OR, None))
                self.addToken(LazyToken())
            else:
                if op == "&&":
                    self.addToken(self.createToken(ExpressionToken.OP_AND, None))
                    self.addToken(LazyToken())
                else:
                    self.addToken(self.createToken(self.OP_TYPE_MAP[op], None))

    def addToken(self, token):
        if token.getType() == ExpressionToken.BRACKET_BEGIN:
            self.status = self.STATUS_BEGIN
        elif token.getType() == ExpressionToken.BRACKET_END:
            self.status = self.STATUS_EXPRESSION
        else:
            self.status = self.STATUS_OPERATOR
        self.previousType = token.getType()
        self.tokens.add(token)

    def addKeyOrObject(self, object, isVar):
        if self.skipSpace(':') and self.isMapMethod():
            self.addToken(self.createToken(ExpressionToken.OP_MAP_PUSH, object))
            self.start += 1
        else:
            if isVar:
                self.addToken(self.createToken(ExpressionToken.VALUE_VAR, object))
            else:
                self.addToken(self.createToken(ExpressionToken.VALUE_CONSTANTS, object))

    def addList(self):
        self.addToken(self.createToken(ExpressionToken.BRACKET_BEGIN, None))
        self.addToken(self.createToken(ExpressionToken.VALUE_NEW_LIST, None))
        if not self.skipSpace(']'):
            self.addToken(self.createToken(ExpressionToken.OP_PARAM_JOIN, None))

    def addMap(self):
        self.addToken(self.createToken(ExpressionToken.BRACKET_BEGIN, None))
        self.addToken(self.createToken(ExpressionToken.VALUE_NEW_MAP, None))

    def findNumber(self):
        i = self.start
        c = self.value.charAt(i += 1)
        if (c == '0') and i < self.end:
            c = self.value.charAt(i += 1)
            if (c == 'x'):
                while i < self.end:
                    c = self.value.charAt(i += 1)
                    if "!(c >= '0' && c <= '9' || c >= 'a' && c <= 'f' || c >= 'A'&& c <= 'F')" is not None:
                        i -= 1
                        break
                return self.parseNumber(self.value.substring(self.start, self.start = i))
            else:
                if "c >= '0' && c <= '9'" is not None:
                    while i < self.end:
                        c = self.value.charAt(i += 1)
                        if c < '0' or c > '7':
                            i -= 1
                            break
                    return self.parseNumber(self.value.substring(self.start, self.start = i))
                else:
                    i -= 1
        while i < self.end:
            c = self.value.charAt(i += 1)
            if c < '0' or c > '9':
                i -= 1
                break
        if (c == '.'):
            i += 1
            while i < self.end:
                c = self.value.charAt(i += 1)
                if c < '0' or c > '9':
                    i -= 1
                    break
        return self.parseNumber(self.value.substring(self.start, self.start = i))

    def parseNumber(self, text):
        if text.startsWith("0x"):
            return Integer.parseInt(text.substring(2), 16)
        else:
            if text.indexOf('.') >= 0:
                return Double.parseDouble(text)
            else:
                if (text.charAt(0) == '0') and len(text) > 1:
                    return Integer.parseInt(text.substring(1), 8)
                else:
                    return Integer.parseInt(text)

    def findId(self):
        p = self.start
        if Character.isJavaIdentifierPart(self.value.charAt(p += 1)):
            while p < self.end:
                if not Character.isJavaIdentifierPart(self.value.charAt(p)):
                    break
                p += 1
            return self.value.substring(self.start, self.start = p)
        raise ExpressionSyntaxException()
            pass
        ExpressionSyntaxException()

    def findString(self):
        quoteChar = self.value.charAt(self.start += 1)
        buf = StringBuilder()
        while self.start < self.end:
            c = self.value.charAt(self.start += 1)
            if c == '\\':
                c2 = self.value.charAt(self.start += 1)
                if c2 == 'b':
                    buf.append('\b')
                    break
                elif c2 == 'f':
                    buf.append('\f')
                    break
                elif c2 == 'n':
                    buf.append('\n')
                    break
                elif c2 == 'r':
                    buf.append('\r')
                    break
                elif c2 == 't':
                    buf.append('\t')
                    break
                elif c2 == 'v':
                    buf.append(0xb)
                    break
                elif c2 == ' ':
                    buf.append(' ')
                    break
                elif c2 == '\\':
                    buf.append('\\')
                    break
                elif c2 == '\'':
                    buf.append('\'')
                    break
                elif c2 == '\"':
                    buf.append('"')
                    break
                elif c2 == 'u':
                    buf.append(Integer.parseInt(self.value.substring(self.start + 1, self.start + 5), 16))
                    self.start += 4
                    break
                elif c2 == 'x':
                    buf.append(Integer.parseInt(self.value.substring(self.start + 1, self.start + 3), 16))
                    self.start += 2
                    break
                break
            elif c == '\'':
                if (c == quoteChar):
                    return str(buf)
            else:
                buf.append(c)
        return

    def skipSpace(self, nextChar):
        while self.start < self.end:
            if not Character.isWhitespace(self.value.charAt(self.start)):
                break
            self.start += 1
        if self.start < self.end:
            next = self.value.charAt(self.start)
            if (nextChar == next):
                return True
        return False

    def get_insertAndReturnIsStatic(self):
        index = len(self.tokens) - 1
        token = self.tokens[index]
        if (token.getType() == ExpressionToken.BRACKET_END):
            depth = 1
            index -= 1
            while index > 0:
                type = token.getType()
                if (type == ExpressionToken.BRACKET_BEGIN):
                    depth -= 1
                    if (depth == 0):
                        return self.insertAndReturnIsStatic(index - 1)
                else:
                    if (type == ExpressionToken.BRACKET_END):
                        depth += 1
                index -= 1
        else:
            if isinstance(token, (VarToken)):
                return self.insertAndReturnIsStatic(index - 1)
            else:
                if isinstance(token, (ValueToken)):
                    return self.insertAndReturnIsStatic(index - 1)
        raise ExpressionSyntaxException("valid call")
            pass
        ExpressionSyntaxException("valid call")

    def set_insertAndReturnIsStatic(self, index):
        if index > 0 and (self.tokens[index].getType() == ExpressionToken.OP_GET_PROP):
            self.tokens.set(index, self.createToken(ExpressionToken.OP_GET_METHOD, None))
            return False
        else:
            self.tokens.add(index, self.createToken(ExpressionToken.OP_GET_STATIC_METHOD, None))
            return True

    insertAndReturnIsStatic = property(get_insertAndReturnIsStatic, set_insertAndReturnIsStatic)

