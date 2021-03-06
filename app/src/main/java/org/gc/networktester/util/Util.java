/*
 *
 * Copyright (C) 2011 Guillaume Cottenceau.
 *
 * Android Network Tester is licensed under the Apache 2.0 license.
 *
 */

package org.gc.networktester.util;

import java.net.ConnectException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Collection;
import java.util.Iterator;

import org.apache.http.conn.ConnectTimeoutException;
import org.gc.networktester.R;
import org.gc.networktester.activity.MainActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.net.ConnectivityManager;
import android.widget.Toast;

public class Util {

    /** Convert an unsigned byte to an int. */ 
    public static int unsignedByteToInt( byte input ) {
        return input < 0 ? input + 256 : input; 
    }

    /**
     * Join the elements of the Collection into a String using the
     * provided separator, with a maximum elements. The type describes how
     * to retrieve the String representation of each element.
     */
    public static <T> String join( Collection<T> col, String sep, int max, String ellipsizeText, String type ) {
        StringBuilder sb = new StringBuilder();
        Iterator<T> li = col.iterator();
        if ( li.hasNext() ) {
            if ( type.equals( "toString" ) ) {
                sb.append( li.next() );
            } else if ( type.equals( "getClass" ) ) {
                sb.append( li.next().getClass().getName() );
            } else {
                sb.append( "???" );
            }
            while ( li.hasNext() ) {
                if ( max == 1 ) {
                    sb.append( ellipsizeText );
                    return sb.toString();
                }
                sb.append( sep );
                if ( type.equals( "toString" ) ) {
                    sb.append( li.next() );
                } else if ( type.equals( "getClass" ) ) {
                    sb.append( li.next().getClass().getName() );
                } else {
                    sb.append( "???" );
                }
                max--;
            }
        }
        return sb.toString();
    }
    
    /**
     * Join the elements of the Collection into a String using the
     * provided separator.
     */
    public static <T> String join( Collection<T> col, String sep ) {
        return join( col, sep, -1, null, "toString" );
    }
   
    /**
     * Retrieve the stacktrace by sending an exception, catching it
     * and getting its stacktrace.
     */
    public static StackTraceElement[] getStackTrace() {
        return new Exception().fillInStackTrace().getStackTrace();
    }

    /**
     * Returns the full backtrace corresponding to the StackTraceElement's
     * passed. This is useful to get the backtrace from a catch block.
     */
    public static String backtraceFull( StackTraceElement[] trace, int from ) {
        StringBuilder sb = new StringBuilder();
        for ( int i = from; i < trace.length; i++ ) {
            sb.append( "\t" ).append( trace[ i ].toString() ).append( "\n" );
        }
        return sb.toString();
    }

    /**
     * Returns the full backtrace of the caller method.
     */
    public static String backtraceFull() {
        return backtraceFull( getStackTrace(), 2 );
    }
 
    /** Go up the chain of exception causes, and print a nice trace of that all. */ 
    public static String printException( Exception e ) { 
        StringBuilder out = new StringBuilder();
        String causePrefix = "";
        Throwable t = e;
        while ( t != null ) {
            out.append( causePrefix )
               .append( "exception: " )
               .append( t )
               .append( " at: " );
            if ( t.getCause() == null ) {
                // no more cause, print full backtrace now because that's the most interesting exception
                out.append( "\n" )
                   .append( backtraceFull( t.getStackTrace(), 0 ) )
                   .append( "\n" );
            } else {
                // there's a cause, print only one line of trace because that is not the most interesting exception
                out.append( t.getStackTrace()[ 0 ] )
                   .append( "\n" );                    
            }
            t = t.getCause();
            // grow the cause prefix 
            causePrefix += "...cause: ";
        }
        return out.toString();
    }

    public static String exceptionMessageOrClass( Exception e ) {
        return e.getMessage() != null && e.getMessage().length() > 0 ? e.getMessage() : e.getClass().getSimpleName();
    }
    
    public static String typicalHttpclientExceptionToString( Context ctx, Exception e ) {
        return e instanceof UnknownHostException ? ctx.getString( R.string.typical_error_unknownhost )
             : e instanceof ConnectTimeoutException ? ctx.getString( R.string.typical_error_connectiontimeout )
             : e instanceof ConnectException ? ctx.getString( R.string.typical_error_connectionrefused )
             : e instanceof SocketTimeoutException ? ctx.getString( R.string.typical_error_sockettimeout )
             : exceptionMessageOrClass( e );
    }

    public static AlertDialog createDialog( final Activity activity, int messageId ) {
        return createDialog( activity, activity.getString( messageId ) );
    }

    public static AlertDialog createDialog( final Activity activity, String message ) {
        AlertDialog.Builder builder = new AlertDialog.Builder( activity );
        builder.setMessage( message ).setPositiveButton( activity.getString( R.string.dialog_ok ), null );
        AlertDialog ad = builder.create();
        ad.show();
        return ad;
    }
    
    private static long[] wifiThresholds = new long[] { 30, 60 };  
    private static long[] mobileThresholds = new long[] { 150, 400 };  

    public static int getTimingResource( MainActivity mainAct, long timing ) {
        if ( mainAct.getNetworkType() == null ) {
            return 0;
        } else if ( mainAct.getNetworkType() == ConnectivityManager.TYPE_MOBILE ) {
            if ( timing < mobileThresholds[ 0 ] ) {
                return R.drawable.timing_good;
            } else if ( timing < mobileThresholds[ 1 ] ) {
                return R.drawable.timing_medium;
            } else {
                return R.drawable.timing_bad;
            }
        } else if ( mainAct.getNetworkType() == ConnectivityManager.TYPE_WIFI
                    || mainAct.getNetworkType() == 6 ) {  // ConnectivityManager.TYPE_WIMAX since API level 8
            if ( timing < wifiThresholds[ 0 ] ) {
                return R.drawable.timing_good;
            } else if ( timing < wifiThresholds[ 1 ] ) {
                return R.drawable.timing_medium;
            } else {
                return R.drawable.timing_bad;
            }
        } else {            
            Toast.makeText( mainAct, R.string.error_unknown_network_type, Toast.LENGTH_LONG ).show();
            return 0;
        } 
    }
    
}