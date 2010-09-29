require 'em-http'
require 'fiber'

# Revert IOC using fibers with EventMachine.
class EMTest
        def initialize
                EventMachine.run {
                        fiber = Fiber.new {
				checkUrls
                        }
                        fiber.resume
               }
        end

        private

	# Check urls synchronously, using asynchronous IO.
	def checkUrls
                urls = %w|http://invalidurl http://bigthink.com/devpatnaik/visionary-businesses-must-play-to-their-strengths http://bigthink.com/ideas/1311|
		urls.each { |url|
			request = head(url)
			if request.nil?
				status = "E"
			else
				status = request.response_header.status.to_s
 			end
			puts "#{status}\t#{url}"
		}
		EventMachine.stop
	end

	# Do a HEAD request
        def head(url)
                fiber = Fiber.current
                request = EventMachine::HttpRequest.new(url).head :redirects => 1

		# avoid error: double resume (FiberError)
                shouldYield =  true
                request.errback {
                        begin
                                fiber.resume(request)
                        rescue FiberError => fe
                                shouldYield = false
                        end
                }

                request.callback {
                        fiber.resume(request)
                }
                if shouldYield
                        Fiber.yield
                end
        end
end

EMTest.new

