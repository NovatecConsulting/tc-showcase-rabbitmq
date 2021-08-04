FROM rabbitmq:3.8-management
RUN rabbitmq-plugins enable --offline rabbitmq_amqp1_0
RUN rabbitmq-plugins enable rabbitmq_shovel_management
