# Exception classes for Sikuli image searching and matching
#
module Rukuli

  # Thrown when Sikuli is unable to find a match within the region for the
  # file given.
  #
  class ImageNotFound < StandardError; end

  # Thrown when a filename is given that is not found on disk in the image
  # path. Image path can be configured using Rukuli::Config.image_path
  #
  class FileDoesNotExist < StandardError; end
end
