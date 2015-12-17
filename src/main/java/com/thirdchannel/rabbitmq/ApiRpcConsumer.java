package com.thirdchannel.rabbitmq;

import com.thirdchannel.rabbitmq.interfaces.EventConsumer;
import com.thirdchannel.rabbitmq.interfaces.RpcConsumer;
import com.thirdchannel.rabbitmq.messages.ApiResponse;

/**
 * @author Steve Pember
 */
public class ApiRpcConsumer extends LagoRpcConsumer<Object, ApiResponse> {
    @Override
    public ApiResponse handleRPC(Object message, RabbitMQDeliveryDetails rabbitMQDeliveryDetails) {
        return buildApi();
    }

    public ApiResponse buildApi() {
        ApiResponse response = new ApiResponse();

        for (EventConsumer consumer : getLago().getRegisteredConsumers()) {
            log.info(consumer.getClass().getSimpleName() + " message: " + consumer.getMessageClass().toString());

            log.info("Instance of Lagorpc? " + (consumer instanceof RpcConsumer));


            response.parseConsumer(consumer);

//            try {
//
//                Method method = consumer.getClass().getMethod("handleMessage", Object.class, RabbitMQDeliveryDetails.class );
//                log.info("Generic Parameter types: ");
//                Class c = method.getParameterTypes()[0];
//                Type t = method.getGenericParameterTypes()[0];
//
//                log.info("Eh");
//            } catch (NoSuchMethodException e) {
//                log.warn("Class " + consumer.getClass().getSimpleName() + " does not contain 'handleMessage'!");
//                log.info("But it does havE: ");
//                for (Method m: consumer.getClass().getMethods()) {
//                    log.info(m.getName() + ": " + m.getParameterTypes().toString());
//                }
//            } catch(ClassCastException | NullPointerException cce) {
//                log.error("Could not cast: ", cce);
//            }
        }

        return response;
    }


}
