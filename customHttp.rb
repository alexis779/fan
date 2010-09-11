require 'socket'
require 'logger'
require 'uri'

class CustomHttp

	@@separator = "\r\n"
	def initialize
		@logger = Logger.new($stderr)
		@logger.level = Logger::DEBUG

		@ips = {}
	end

	def get(url)
		uri = URI.parse(url)

		path = uri.path
		unless uri.query.nil?
			path << "?" + uri.query
		end
		# TODO: check if path is empty

		method = "HEAD"
		puts httpRequest(uri.host, uri.port, uri.path, method)
	end

	def httpRequest(host, port, path, method)

		# TODO: add User-Agent
		# TODO: specify HTTP version
		request =
%Q|#{method} #{path} HTTP/1.1
Host: #{host}
Connection: keep-alive

|
		@logger.debug("Getting connection on #{host}:#{port}")
		socket = openSocket(host, port)
		@logger.debug("#{method} #{path}")
		socket.print(request)

		socket.sync = true
		statusLine = socket.readline
		headers = []
		
		begin
			headers << socket.read(1024 * 16)
			p headers.last
		rescue EOFError
		end

		socket.close

		if headers.empty?
			return nil
		end
		exit
=begin
		fileWriter = open("body", "w")
		fileWriter.print(body)
		fileWriter.close
=end


		httpVersion, statusCode, status = statusLine.split
		@logger.debug("#{statusCode} #{status}")

		if statusCode == "302"
			headers.each { |header|
				puts header
				if header =~ /^Location: /
					location = $'
					# TODO check if location is absolute
					# TODO set a limit in the recursion
					statusCode = httpRequest(host, port, location, method)
				end
			}
		end

		statusCode
	end

	private

	def openSocket(host, port)
		ip = @ips[host]
		if ip.nil?
			ip = IPSocket::getaddress(host)
			@ips[host] = ip
			@logger.debug("IP address for #{host}: " + ip)
		end

		TCPSocket.open(ip, port)
	end

	# IPSocket::getaddress(host)
	def resolveIp(host)
		@logger.debug("DNS lookup of #{host}")

		# TODO: check if host is already an IP
		hostname, aliases, addressFamily, address = Socket::gethostbyname(host)
		# TODO: manage IPv6
		address.unpack('C*').join(".")
	end

end

http = CustomHttp.new
http.get("http://www.dailymotion.com/")
#http.get("http://www.truveo.com/")
#http.get("http://www.hotmail.com/")

