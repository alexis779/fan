require 'uri'
require 'rubygems'
require 'logger'
require 'em-http'

class EventMachineHttp

	def initialize
		@logger = Logger.new($stderr)
		@logger.level = Logger::DEBUG
	end

	def get(url)
		method = "HEAD"
		httpRequest(url, method)
	end

	def httpRequest(url, method)
		uri = URI.parse(url)
		host = uri.host
		port = uri.port
		path = uri.path
		unless uri.query.nil?
			path << "?" + uri.query
		end

		unless method == "HEAD"
			# TODO: implement other methods
			return nil
		end
		@logger.debug("Getting connection on #{host}:#{port}")

		request = EventMachine::HttpRequest.new(url).head :redirects => 1
		request.callback {
			@logger.info(request.response_header.status)
			EventMachine.stop
		}

		request.errback { |e|
			@logger.warn(e)
		}

	end

end

http = EventMachineHttp.new
EventMachine::run {
	http.get("http://www.dailymotion.com/")
}

