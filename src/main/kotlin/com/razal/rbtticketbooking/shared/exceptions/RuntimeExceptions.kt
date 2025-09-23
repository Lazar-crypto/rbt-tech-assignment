package com.razal.rbtticketbooking.shared.exceptions

class EntityAlreadyExistsException(message: String) : RuntimeException(message)
class ResourceNotFoundException(message: String) : RuntimeException(message)
class DummyJsonException(message: String) : RuntimeException(message)
class TicketHoldException(message: String) : RuntimeException(message)