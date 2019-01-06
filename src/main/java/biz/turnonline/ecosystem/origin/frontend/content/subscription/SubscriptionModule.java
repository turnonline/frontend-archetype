/*
 * Copyright 2018 Comvai, s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package biz.turnonline.ecosystem.origin.frontend.content.subscription;

import biz.turnonline.ecosystem.origin.frontend.content.ContentSubscription;
import biz.turnonline.ecosystem.origin.frontend.content.model.CommonContent;
import biz.turnonline.ecosystem.origin.frontend.content.model.EventContent;
import biz.turnonline.ecosystem.origin.frontend.content.model.MallContent;
import biz.turnonline.ecosystem.origin.frontend.content.model.PayInvoiceContent;
import biz.turnonline.ecosystem.origin.frontend.content.model.ProductContent;
import biz.turnonline.ecosystem.origin.frontend.content.model.TermsContent;
import biz.turnonline.ecosystem.origin.frontend.content.subscription.adaptee.CommonContentLocalAdaptee;
import biz.turnonline.ecosystem.origin.frontend.content.subscription.adaptee.EventContentLocalAdaptee;
import biz.turnonline.ecosystem.origin.frontend.content.subscription.adaptee.MallContentLocalAdaptee;
import biz.turnonline.ecosystem.origin.frontend.content.subscription.adaptee.PayInvoiceContentLocalAdaptee;
import biz.turnonline.ecosystem.origin.frontend.content.subscription.adaptee.ProductContentLocalAdaptee;
import biz.turnonline.ecosystem.origin.frontend.content.subscription.adaptee.RemoveContentCacheTask;
import biz.turnonline.ecosystem.origin.frontend.content.subscription.adaptee.TermsContentLocalAdaptee;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.eventbus.EventBus;
import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import com.googlecode.objectify.ObjectifyService;
import org.ctoolkit.restapi.client.adaptee.GetExecutorAdaptee;
import org.ctoolkit.restapi.client.provider.LocalResourceProvider;
import org.ctoolkit.restapi.client.pubsub.PubsubMessageListener;
import org.ctoolkit.restapi.client.pubsub.SubscriptionsListenerModule;

import javax.inject.Singleton;
import java.util.Locale;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * Guice subscription configuration.
 *
 * @author <a href="mailto:medvegy@turnonline.biz">Aurel Medvegy</a>
 */
public class SubscriptionModule
        extends AbstractModule
{
    private final Locale defaultLocale;

    /**
     * The default locale that will be used for local content caching and retrieval if none was received.
     *
     * @param defaultLocale the default locale
     */
    public SubscriptionModule( Locale defaultLocale )
    {
        this.defaultLocale = checkNotNull( defaultLocale );
    }

    @Override
    protected void configure()
    {
        install( new SubscriptionsListenerModule() );

        String busName = "turnon.content.subscription";
        bind( EventBus.class ).annotatedWith( ContentSubscription.class ).toInstance( new EventBus( busName ) );
        bind( Locale.class ).annotatedWith( ContentSubscription.class ).toInstance( defaultLocale );
        bind( ContentNaming.class ).in( Singleton.class );

        MapBinder<String, PubsubMessageListener> map;
        map = MapBinder.newMapBinder( binder(), String.class, PubsubMessageListener.class );
        map.addBinding( "turnon.content" ).to( TurnOnContentSubscription.class );

        // caching
        bind( new TypeLiteral<LocalResourceProvider<CommonContent>>()
        {
        } ).to( CommonContentLocalAdaptee.class );

        bind( new TypeLiteral<LocalResourceProvider<PayInvoiceContent>>()
        {
        } ).to( PayInvoiceContentLocalAdaptee.class );

        bind( new TypeLiteral<LocalResourceProvider<ProductContent>>()
        {
        } ).to( ProductContentLocalAdaptee.class );

        bind( new TypeLiteral<LocalResourceProvider<EventContent>>()
        {
        } ).to( EventContentLocalAdaptee.class );

        bind( new TypeLiteral<LocalResourceProvider<TermsContent>>()
        {
        } ).to( TermsContentLocalAdaptee.class );

        bind( new TypeLiteral<LocalResourceProvider<MallContent>>()
        {
        } ).to( MallContentLocalAdaptee.class );

        // adaptees
        bind( new TypeLiteral<GetExecutorAdaptee<CommonContent>>()
        {
        } ).to( CommonContentLocalAdaptee.class );

        bind( new TypeLiteral<GetExecutorAdaptee<PayInvoiceContent>>()
        {
        } ).to( PayInvoiceContentLocalAdaptee.class );

        bind( new TypeLiteral<GetExecutorAdaptee<ProductContent>>()
        {
        } ).to( ProductContentLocalAdaptee.class );

        bind( new TypeLiteral<GetExecutorAdaptee<EventContent>>()
        {
        } ).to( EventContentLocalAdaptee.class );

        bind( new TypeLiteral<GetExecutorAdaptee<TermsContent>>()
        {
        } ).to( TermsContentLocalAdaptee.class );

        bind( new TypeLiteral<GetExecutorAdaptee<MallContent>>()
        {
        } ).to( MallContentLocalAdaptee.class );

        ObjectifyService.register( RawCommonContent.class );
        ObjectifyService.register( RawMallArticle.class );
        ObjectifyService.register( RawPayInvoiceContent.class );
        ObjectifyService.register( RawProductContent.class );
        ObjectifyService.register( RawEventContent.class );
        ObjectifyService.register( RawTermsContent.class );
        requestStaticInjection( RemoveContentCacheTask.class );
    }

    @Provides
    @Singleton
    @ContentSubscription
    ObjectMapper provideObjectMapper()
    {
        JsonFactory factory = new JsonFactory();
        factory.enable( JsonParser.Feature.ALLOW_COMMENTS );

        ObjectMapper mapper = new ObjectMapper( factory );
        mapper.setSerializationInclusion( JsonInclude.Include.NON_NULL );
        mapper.configure( DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false );

        return mapper;
    }
}