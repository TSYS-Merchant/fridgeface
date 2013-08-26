function addEvent(obj, evt, fn)
{
	if (obj.addEventListener)
		obj.addEventListener(evt, fn, false);
	else if (obj.attachEvent)
		obj.attachEvent('on'+evt, fn);
	else
		obj['on'+evt] = fn;
}

function removeEvent(obj, evt, fn)
{
	if (obj.removeEventListener)
		obj.removeEventListener(evt, fn, false);
	else if (obj.detachEvent)
		obj.detachEvent('on'+evt, fn);
	else
		obj['on'+evt] = null;
}

Function.prototype.partial = function(/* 0..n args */)
{
    var fn = this, args = Array.prototype.slice.call(arguments);
    return function()
    {
        var arg = 0;
        for(var i = 0; i < args.length && arg < arguments.length; i++)
        {
            if(args[i] === undefined)
            args[i] = arguments[arg++];
        }
        return fn.apply(this, args);
    };
}

Function.prototype.bind = function(object)
{
	var fn = this;
	var args = Array.prototype.slice.apply(arguments).slice.apply(arguments, [1]);
	return function()
	{
		return fn.apply(object, args);
	}; 
};

Function.prototype.defer = function(condition, poll_interval)
{
	var interval;
	
	if(!poll_interval)
		poll_interval=100;
	
	if(typeof(condition) != 'function') // Just wait until the DOM is loaded
	{
		setTimeout(this, 0); // This will make it fire after DOM load. Who knew?
		return;
	}
	
	if(condition())
		this();
	else
	{
		interval=setInterval(function()
		{
			if(condition())
			{
				clearInterval(interval);
				this();
			}
		}.bind(this), poll_interval);
	}
	
	return this;
};

document.getElementsByClassName = function(className)
{
	var classes = className.split(' ');
	var classesToCheck = '';
	var returnElements = [];
	var match, node, elements;
	
	if (document.evaluate)
	{    
		var xhtmlNamespace = 'http://www.w3.org/1999/xhtml';
		var namespaceResolver = (document.documentElement.namespaceURI === xhtmlNamespace)? xhtmlNamespace:null;
		
		for(var j=0, jl=classes.length; j<jl;j+=1)
			classesToCheck += "[contains(concat(' ', @class, ' '), ' " + classes[j] + " ')]"; 
		
		try
		{
			elements = document.evaluate(".//*" + classesToCheck, document, namespaceResolver, 0, null);
		}
		catch(err)
		{
			elements = document.evaluate(".//*" + classesToCheck, document, null, 0, null);
		}

		while((match = elements.iterateNext()))
			returnElements.push(match);
	}
	else
	{
		classesToCheck = [];
		elements = (document.all) ? document.all : document.getElementsByTagName("*");
		
		for (var k=0, kl=classes.length; k<kl; k+=1)
			classesToCheck.push(new RegExp("(^|\\s)" + classes[k] + "(\\s|$)"));
		
		for (var l=0, ll=elements.length; l<ll;l+=1)
		{
			node = elements[l];
			match = false;
			for (var m=0, ml=classesToCheck.length; m<ml; m+=1)
			{
				match = classesToCheck[m].test(node.className);
				if (!match) break;
			}
			if (match) returnElements.push(node);
		}
	}
	return returnElements;
};
