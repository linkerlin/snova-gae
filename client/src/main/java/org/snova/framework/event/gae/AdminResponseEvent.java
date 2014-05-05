/**
 * 
 */
package org.snova.framework.event.gae;

import java.io.IOException;

import org.arch.buffer.Buffer;
import org.arch.buffer.BufferHelper;
import org.arch.event.Event;
import org.arch.event.EventType;
import org.arch.event.EventVersion;
import org.snova.framework.event.CommonEventConstants;


/**
 * @author qiyingwang
 * 
 */
@EventType(CommonEventConstants.ADMIN_RESPONSE_EVENT_TYPE)
@EventVersion(1)
public class AdminResponseEvent extends Event
{
	public AdminResponseEvent()
	{
	}

	public AdminResponseEvent(String response, String errorCause, int errno)
	{
		this.response = response;
		this.errorCause = errorCause;
		this.errno = errno;
	}

	public String response;
	public String errorCause;
	public int errno;

	@Override
	protected boolean onDecode(Buffer buffer)
	{
		try
		{
			response = BufferHelper.readVarString(buffer);
			errorCause = BufferHelper.readVarString(buffer);
			errno = BufferHelper.readVarInt(buffer);
			return true;
		}
		catch (IOException e)
		{
			return false;
		}
	}

	@Override
	protected boolean onEncode(Buffer buffer)
	{
		BufferHelper.writeVarString(buffer, response);
		BufferHelper.writeVarString(buffer, errorCause);
		BufferHelper.writeVarInt(buffer, errno);
		return true;
	}

}
