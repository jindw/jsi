#!/usr/bin/env python
# -*- coding: utf-8 -*-
VALUE_CONSTANTS = 0
VALUE_VAR = 1
VALUE_NEW_MAP = 2
VALUE_NEW_LIST = 3
VALUE_LAZY = 4
BRACKET_BEGIN = 5
BRACKET_END = 6
OP_ADD = 10
OP_SUB = 11
OP_MUL = 12
OP_DIV = 13
OP_MOD = 14
OP_QUESTION = 15
OP_QUESTION_SELECT = 16
OP_GET_PROP = 17
OP_LT = 18
OP_GT = 19
OP_LTEQ = 20
OP_GTEQ = 21
OP_EQ = 22
OP_NOTEQ = 23
OP_AND = 24
OP_OR = 25
OP_NOT = 26
OP_POS = 27
OP_NEG = 28
OP_GET_METHOD = 29
OP_GET_STATIC_METHOD = 30
OP_INVOKE_METHOD = 31
OP_PARAM_JOIN = 32
OP_MAP_PUSH = 33
class ExpressionToken(object):
    """ generated source for ExpressionToken
    """
    def getType(self):
        raise NotImplementedError()

    def toString(self):
        raise NotImplementedError()



class VarToken(ExpressionToken):
    """ generated source for VarToken

    """
    value = ""

    def __init__(self, value):
        self.value = self.value

    def getType(self):
        return VALUE_VAR

    def getValue(self):
        return self.value

    def toString(self):
        return self.value


